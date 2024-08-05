import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class User {
    private String username;
    private String password;
    private String role;

    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}

class Candidate {
    private String name;
    private int votes;

    public Candidate(String name) {
        this.name = name;
        this.votes = 0;
    }

    public String getName() {
        return name;
    }

    public int getVotes() {
        return votes;
    }

    public void addVote() {
        this.votes++;
    }

    @Override
    public String toString() {
        return "Candidate{" +
                "name='" + name + '\'' +
                ", votes=" + votes +
                '}';
    }
}

class AuthService {
    private List<User> users = new ArrayList<>();
    private String[][] userCredentials = new String[100][2];
    private int userCount = 0;
    private static final String FILE_NAME = "users.txt";

    public AuthService() {
        loadUsersFromFile();
    }

    public User authenticate(String username, String password) {
        for (int i = 0; i < userCount; i++) {
            if (userCredentials[i][0].equals(username) && userCredentials[i][1].equals(password)) {
                for (User user : users) {
                    if (user.getUsername().equals(username)) {
                        return user;
                    }
                }
            }
        }
        return null;
    }

    public void addUser(User user) {
        users.add(user);
        userCredentials[userCount][0] = user.getUsername();
        userCredentials[userCount][1] = user.getPassword();
        userCount++;
        saveUsersToFile();
    }

    public boolean isUsernameTaken(String username) {
        for (int i = 0; i < userCount; i++) {
            if (userCredentials[i][0].equals(username)) {
                return true;
            }
        }
        return false;
    }

    private void saveUsersToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (User user : users) {
                writer.write(user.getUsername() + "," + user.getPassword() + "," + user.getRole());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving users to file: " + e.getMessage());
        }
    }

    private void loadUsersFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    User user = new User(parts[0], parts[1], parts[2]);
                    addUser(user);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading users from file: " + e.getMessage());
        }
    }

    public List<User> getUsers() {
        return users;
    }
}

class RegistrationService {
    private AuthService authService;

    public RegistrationService(AuthService authService) {
        this.authService = authService;
    }

    public boolean registerUser(String username, String password, String role) {
        if (authService.isUsernameTaken(username)) {
            return false;
        }
        User user = new User(username, password, role);
        authService.addUser(user);
        return true;
    }
}

class VotingService {
    private List<Candidate> candidates = new ArrayList<>();
    private static final String VOTES_FILE = "votes.txt";
    private static final String VOTES_CAST_FILE = "votes_cast.txt";

    public VotingService() {
        addCandidate("Candidate 1");
        addCandidate("Candidate 2");
        addCandidate("Candidate 3");
        loadVotesFromFile();
        loadVotesCastFromFile();
    }

    public void addCandidate(String name) {
        candidates.add(new Candidate(name));
    }

    public List<Candidate> getCandidates() {
        return candidates;
    }

    public void vote(User user, String candidateName) {
        for (Candidate candidate : candidates) {
            if (candidate.getName().equals(candidateName)) {
                candidate.addVote();
                saveVotesToFile();
                saveVoteCast(user.getUsername(), candidateName);
                System.out.println(user.getUsername() + " voted for " + candidateName);
                return;
            }
        }
        System.out.println("Candidate not found.");
    }

    private void saveVotesToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(VOTES_FILE))) {
            for (Candidate candidate : candidates) {
                writer.write(candidate.getName() + "," + candidate.getVotes());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving votes to file: " + e.getMessage());
        }
    }

    private void loadVotesFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(VOTES_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    String name = parts[0];
                    int votes = Integer.parseInt(parts[1]);
                    for (Candidate candidate : candidates) {
                        if (candidate.getName().equals(name)) {
                            while (votes-- > 0) {
                                candidate.addVote();
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading votes from file: " + e.getMessage());
        }
    }

    private void saveVoteCast(String username, String candidateName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(VOTES_CAST_FILE, true))) {
            writer.write(username + " voted for " + candidateName);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Error saving vote cast to file: " + e.getMessage());
        }
    }

    private void loadVotesCastFromFile() {
        // To implement if needed to load past votes cast
    }

    public void viewVotesCast() {
        try (BufferedReader reader = new BufferedReader(new FileReader(VOTES_CAST_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.err.println("Error reading votes cast file: " + e.getMessage());
        }
    }

    public void modifyCandidate(String oldName, String newName) {
        for (Candidate candidate : candidates) {
            if (candidate.getName().equals(oldName)) {
                candidate = new Candidate(newName); // Replace old candidate with new candidate
                saveVotesToFile();
                System.out.println("Candidate info modified.");
                return;
            }
        }
        System.out.println("Candidate not found.");
    }

    public void removeCandidate(String name) {
        candidates.removeIf(candidate -> candidate.getName().equals(name));
        saveVotesToFile();
        System.out.println("Candidate removed.");
    }
}

public class Main {
    public static void main(String[] args) {
        AuthService authService = new AuthService();
        RegistrationService registrationService = new RegistrationService(authService);
        VotingService votingService = new VotingService();
        Scanner scanner = new Scanner(System.in);

        boolean running = true;
        while (running) {
            System.out.println("Welcome to the Online Voting System");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");
            int option = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (option) {
                case 1:
                    System.out.print("Enter username: ");
                    String username = scanner.nextLine();
                    System.out.print("Enter password: ");
                    String password = scanner.nextLine();
                    System.out.print("Enter role (voter/admin): ");
                    String role = scanner.nextLine();

                    boolean success = registrationService.registerUser(username, password, role);
                    if (success) {
                        System.out.println("Registration successful. You can now log in.");
                    } else {
                        System.out.println("Username already exists. Please try a different username.");
                    }
                    break;
                case 2:
                    System.out.print("Enter username: ");
                    username = scanner.nextLine();
                    System.out.print("Enter password: ");
                    password = scanner.nextLine();

                    if ("admin".equals(username) && "667".equals(password)) {
                        boolean adminRunning = true;
                        while (adminRunning) {
                            System.out.println("Admin login successful.");
                            System.out.println("1. View all users");
                            System.out.println("2. View vote counts");
                            System.out.println("3. View votes cast");
                            System.out.println("4. Modify candidate info");
                            System.out.println("5. Logout");
                            System.out.print("Choose an option: ");
                            int adminOption = scanner.nextInt();
                            scanner.nextLine(); // consume newline

                            switch (adminOption) {
                                case 1:
                                    List<User> users = authService.getUsers();
                                    System.out.println("Registered users:");
                                    for (User user : users) {
                                        System.out.println(user);
                                    }System.out.println(" ");
                                    break;
                                case 2:
                                    List<Candidate> candidates = votingService.getCandidates();
                                    System.out.println("Vote counts:");
                                    for (Candidate candidate : candidates) {
                                        System.out.println(candidate);
                                    }System.out.println(" ");
                                    break;
                                case 3:
                                    System.out.println("Votes cast:");
                                    votingService.viewVotesCast();
                                    System.out.println(" ");
                                    break;
                                case 4:
                                    System.out.println("Modify candidate info:");
                                    System.out.println("1. Add candidate");
                                    System.out.println("2. Remove candidate");
                                    System.out.println("3. Rename candidate");
                                    System.out.print("Choose an option: ");
                                    int modifyOption = scanner.nextInt();
                                    scanner.nextLine(); // consume newline
                                    if (modifyOption == 1) {
                                        System.out.print("Enter new candidate name: ");
                                        String newCandidateName = scanner.nextLine();
                                        votingService.addCandidate(newCandidateName);
                                        System.out.println("Candidate added.");
                                        System.out.println(" ");
                                    } else if (modifyOption == 2) {
                                        System.out.print("Enter candidate name to remove: ");
                                        String candidateNameToRemove = scanner.nextLine();
                                        votingService.removeCandidate(candidateNameToRemove);
                                        System.out.println(" ");
                                    } else if (modifyOption == 3) {
                                        System.out.print("Enter current candidate name: ");
                                        String currentName = scanner.nextLine();
                                        System.out.print("Enter new candidate name: ");
                                        String newName = scanner.nextLine();
                                        votingService.modifyCandidate(currentName, newName);
                                        System.out.println(" ");
                                    } else {
                                        System.out.println("Invalid option.");
                                        System.out.println(" ");
                                    }
                                    break;
                                case 5:
                                    adminRunning = false;
                                    System.out.println("Admin logged out.");
                                    System.out.println(" ");
                                    break;
                                default:
                                    System.out.println("Invalid option. Please choose again.");
                                    System.out.println(" ");
                            }
                        }
                    } else {
                        User user = authService.authenticate(username, password);
                        if (user != null) {
                            System.out.println("Welcome " + user.getUsername() + " (" + user.getRole() + ")");
                            if ("voter".equals(user.getRole())) {
                                System.out.println("Do you want to vote? (yes/no): ");
                                String response = scanner.nextLine();
                                if ("yes".equalsIgnoreCase(response)) {
                                    System.out.println("Candidates:");
                                    List<Candidate> candidates = votingService.getCandidates();
                                    for (Candidate candidate : candidates) {
                                        System.out.println(candidate.getName());
                                    }
                                    System.out.print("Enter candidate name to vote: ");
                                    String candidateName = scanner.nextLine();
                                    votingService.vote(user, candidateName);
                                }
                            }
                        } else {
                            System.out.println("Invalid credentials.");
                        }
                    }
                    break;
                case 3:
                    running = false;
                    System.out.println("Exiting the Online Voting System. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid option. Please choose again.");
            }
            System.out.println(); // Add a blank line for better readability
        }

        scanner.close();
    }
}
